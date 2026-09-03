import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenerateResourceRequests } from './generate-resource-requests';

describe('GenerateResourceRequests', () => {
  let component: GenerateResourceRequests;
  let fixture: ComponentFixture<GenerateResourceRequests>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenerateResourceRequests],
    }).compileComponents();

    fixture = TestBed.createComponent(GenerateResourceRequests);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
